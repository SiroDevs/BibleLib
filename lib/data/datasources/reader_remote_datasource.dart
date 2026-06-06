import '../../core/errors/exceptions.dart';
import '../../core/network/api_client.dart';
import '../local/entities/chapter_entity.dart';
import '../local/entities/verse_entity.dart';

abstract class ReaderRemoteDataSource {
  Future<List<VerseEntity>> getChapterVerses(
    String bibleId,
    String chapterId,
  );

  Future<ChapterEntity> getChapter(String bibleId, String chapterId);
}

class ReaderRemoteDataSourceImpl implements ReaderRemoteDataSource {
  final ApiClient _client;
  const ReaderRemoteDataSourceImpl(this._client);

  @override
  Future<List<VerseEntity>> getChapterVerses(
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
            (json) => VerseEntity.fromJson(
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
  Future<ChapterEntity> getChapter(String bibleId, String chapterId) async {
    try {
      final response = await _client.get(
        '/bibles/$bibleId/chapters/$chapterId',
      );
      final data = response['data'] as Map<String, dynamic>? ?? {};
      return ChapterEntity.fromJson(data, bibleId: bibleId);
    } catch (e) {
      if (e is ServerException || e is NetworkException) rethrow;
      throw ServerException(e.toString());
    }
  }
}
