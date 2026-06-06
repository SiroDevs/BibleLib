// lib/core/utils/bible_download_worker.dart

import 'dart:async';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:workmanager/workmanager.dart';

import '../constants/app_constants.dart';
import '../network/api_client.dart';
import '../../data/sources/local/database/app_database.dart';
import '../../data/entities/chapter_entity.dart';
import '../../data/entities/verse_entity.dart';

const _kTaskName = 'com.biblelib.bible-download';
const _kInputBibleIds = 'bibleIds';

const _kBookIds = [
  'GEN', 'EXO', 'LEV', 'NUM', 'DEU', 'JOS', 'JDG', 'RUT',
  '1SA', '2SA', '1KI', '2KI', '1CH', '2CH', 'EZR', 'NEH',
  'EST', 'JOB', 'PSA', 'PRO', 'ECC', 'SNG', 'ISA', 'JER',
  'LAM', 'EZK', 'DAN', 'HOS', 'JOL', 'AMO', 'OBA', 'JON',
  'MIC', 'NAM', 'HAB', 'ZEP', 'HAG', 'ZEC', 'MAL',
  'MAT', 'MRK', 'LUK', 'JHN', 'ACT', 'ROM', '1CO', '2CO',
  'GAL', 'EPH', 'PHP', 'COL', '1TH', '2TH', '1TI', '2TI',
  'TIT', 'PHM', 'HEB', 'JAS', '1PE', '2PE', '1JN', '2JN',
  '3JN', 'JUD', 'REV',
];

// Top-level function required by WorkManager — referenced in main.dart with
// @pragma('vm:entry-point').
void bibleLibCallbackDispatcher() {
  Workmanager().executeTask((taskName, inputData) async {
    if (taskName != _kTaskName) return true;
    try {
      await dotenv.load(fileName: '.env');
      final ids = (inputData?[_kInputBibleIds] as String? ?? '').split(',');
      final client = ApiClient();
      final db = await AppDatabase.create();
      await _downloadAll(ids, client, db);
      return true;
    } catch (e) {
      debugPrint('[BibleDownloadWorker] error: $e');
      return false;
    }
  });
}

abstract final class BibleDownloadWorker {
  static Future<void> initialize() async {
    await Workmanager().initialize(
      bibleLibCallbackDispatcher,
      isInDebugMode: false,
    );
  }

  static Future<void> enqueue(List<String> bibleIds) async {
    await Workmanager().registerOneOffTask(
      _kTaskName,
      _kTaskName,
      inputData: {_kInputBibleIds: bibleIds.join(',')},
      constraints: Constraints(networkType: NetworkType.connected),
      backoffPolicy: BackoffPolicy.exponential,
      backoffPolicyDelay: const Duration(seconds: 30),
    );
  }

  static Future<void> downloadForeground({
    required List<String> bibleIds,
    required ApiClient client,
    required AppDatabase db,
    void Function(int done, int total)? onProgress,
  }) =>
      _downloadAll(bibleIds, client, db, onProgress: onProgress);
}

Future<void> _downloadAll(
  List<String> bibleIds,
  ApiClient client,
  AppDatabase db, {
  void Function(int done, int total)? onProgress,
}) async {
  for (final bibleId in bibleIds) {
    await _downloadBible(bibleId, client, db, onProgress: onProgress);
  }
}

Future<void> _downloadBible(
  String bibleId,
  ApiClient client,
  AppDatabase db, {
  void Function(int done, int total)? onProgress,
}) async {
  int done = 0;
  for (final bookId in _kBookIds) {
    try {
      await _downloadBook(bibleId, bookId, client, db);
    } catch (e) {
      debugPrint('[BibleDownloadWorker] skipped $bookId: $e');
    }
    done++;
    onProgress?.call(done, _kBookIds.length);
  }
}

Future<void> _downloadBook(
  String bibleId,
  String bookId,
  ApiClient client,
  AppDatabase db,
) async {
  final chaptersRes =
      await client.get('/bibles/$bibleId/books/$bookId/chapters');
  final chaptersData =
      (chaptersRes['data'] as List<dynamic>? ?? []).cast<Map<String, dynamic>>();

  final chapterModels = chaptersData
      .where((c) => (c['id'] as String? ?? '').isNotEmpty && c['id'] != 'intro')
      .map((c) => ChapterEntity.fromJson(c, bibleId: bibleId))
      .toList();

  if (chapterModels.isEmpty) return;
  await db.chapterDao.insertChapters(chapterModels);

  final batches = _chunk(chapterModels, kBatchSize);
  for (final batch in batches) {
    await Future.wait(
      batch.map(
        (ch) => _downloadChapterVerses(bibleId, ch.id, bookId, client, db),
      ),
    );
    await Future<void>.delayed(const Duration(milliseconds: 250));
  }
}

Future<void> _downloadChapterVerses(
  String bibleId,
  String chapterId,
  String bookId,
  ApiClient client,
  AppDatabase db,
) async {
  final cached = await db.verseDao.countVersesInChapter(bibleId, chapterId);
  if ((cached ?? 0) > 0) return;

  final res = await client.get(
    '/bibles/$bibleId/chapters/$chapterId/verses',
    queryParameters: {'content-type': 'text', 'include-verse-numbers': true},
  );
  final data =
      (res['data'] as List<dynamic>? ?? []).cast<Map<String, dynamic>>();

  final models = data
      .map(
        (json) => VerseEntity.fromJson(
          json,
          bibleId: bibleId,
          bookId: bookId,
          chapterId: chapterId,
        ),
      )
      .toList();

  if (models.isNotEmpty) await db.verseDao.insertVerses(models);
}

List<List<T>> _chunk<T>(List<T> list, int size) {
  final chunks = <List<T>>[];
  for (int i = 0; i < list.length; i += size) {
    chunks.add(list.sublist(i, min(i + size, list.length)));
  }
  return chunks;
}
