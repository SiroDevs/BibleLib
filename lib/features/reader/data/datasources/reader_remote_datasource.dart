// lib/features/reader/data/datasources/reader_remote_datasource.dart

import 'package:biblelib/core/errors/exceptions.dart';
import 'package:biblelib/core/network/api_client.dart';
import 'package:biblelib/features/reader/data/models/chapter_model.dart';
import 'package:biblelib/features/reader/data/models/verse_model.dart';

abstract class ReaderRemoteDataSource {
  Future<List<VerseModel>> getChapterVerses(
    String bibleId,
    String chapterId,
  );

  Future<ChapterModel> getChapter(String bibleId, String chapterId);
}

class ReaderRemoteDataSourceImpl implements ReaderRemoteDataSource {
  final ApiClient _client;
  const ReaderRemoteDataSourceImpl(this._client);

  @override
  Future<List<VerseModel>> getChapterVerses(
    String bibleId,
    String chapterId,
  ) async {
    try {
      final response = await _client.get(
        '/bibles/$bibleId/chapters/$chapterId/verses',
        queryParameters: {'content-type': 'text', 'include-verse-numbers': true},
      );
      final data = response['data'] as List<dynamic>? ?? [];
      final bookId = chapterId.split('.').first;
      return data.cast<Map<String, dynamic>>().map(
            (json) => VerseModel.fromJson(
              json,
              bibleId: bibleId,
              bookId: bookId,
              chapterId: chapterId,
            ),
          ).toList();
    } catch (e) {
      if (e is ServerException || e is NetworkException) rethrow;
      throw ServerException(e.toString());
    }
  }

  @override
  Future<ChapterModel> getChapter(String bibleId, String chapterId) async {
    try {
      final response = await _client.get(
        '/bibles/$bibleId/chapters/$chapterId',
      );
      final data = response['data'] as Map<String, dynamic>? ?? {};
      return ChapterModel.fromJson(data, bibleId: bibleId);
    } catch (e) {
      if (e is ServerException || e is NetworkException) rethrow;
      throw ServerException(e.toString());
    }
  }
}
