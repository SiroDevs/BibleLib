// lib/features/reader/data/datasources/reader_local_datasource.dart

import 'package:biblelib/core/errors/exceptions.dart';
import 'package:biblelib/core/network/local/daos/chapter_dao.dart';
import 'package:biblelib/core/network/local/daos/verse_dao.dart';
import 'package:biblelib/features/reader/data/models/chapter_model.dart';
import 'package:biblelib/features/reader/data/models/verse_model.dart';

abstract class ReaderLocalDataSource {
  Future<List<VerseModel>> getCachedVerses(String bibleId, String chapterId);
  Future<ChapterModel?> getCachedChapter(String bibleId, String chapterId);
  Future<void> cacheVerses(List<VerseModel> verses);
  Future<void> cacheChapter(ChapterModel chapter);
  Future<bool> hasChapterCached(String bibleId, String chapterId);
}

class ReaderLocalDataSourceImpl implements ReaderLocalDataSource {
  final VerseDao _verseDao;
  final ChapterDao _chapterDao;

  const ReaderLocalDataSourceImpl(this._verseDao, this._chapterDao);

  @override
  Future<List<VerseModel>> getCachedVerses(
    String bibleId,
    String chapterId,
  ) async {
    try {
      return await _verseDao.getVersesByChapter(bibleId, chapterId);
    } catch (e) {
      throw CacheException('Failed to load cached verses: $e');
    }
  }

  @override
  Future<ChapterModel?> getCachedChapter(
    String bibleId,
    String chapterId,
  ) async {
    try {
      return await _chapterDao.getChapter(chapterId, bibleId);
    } catch (e) {
      throw CacheException('Failed to load cached chapter: $e');
    }
  }

  @override
  Future<void> cacheVerses(List<VerseModel> verses) async {
    try {
      await _verseDao.insertVerses(verses);
    } catch (e) {
      throw CacheException('Failed to cache verses: $e');
    }
  }

  @override
  Future<void> cacheChapter(ChapterModel chapter) async {
    try {
      await _chapterDao.insertChapters([chapter]);
    } catch (e) {
      throw CacheException('Failed to cache chapter: $e');
    }
  }

  @override
  Future<bool> hasChapterCached(String bibleId, String chapterId) async {
    final count = await _verseDao.countVersesInChapter(bibleId, chapterId);
    return (count ?? 0) > 0;
  }
}
