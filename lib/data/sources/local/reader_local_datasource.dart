import '../../../core/errors/exceptions.dart';
import '../../entities/chapter_entity.dart';
import '../../entities/verse_entity.dart';
import 'daos/chapter_dao.dart';
import 'daos/verse_dao.dart';

abstract class ReaderLocalDataSource {
  Future<List<VerseEntity>> getCachedVerses(String bibleId, String chapterId);
  Future<ChapterEntity?> getCachedChapter(String bibleId, String chapterId);
  Future<void> cacheVerses(List<VerseEntity> verses);
  Future<void> cacheChapter(ChapterEntity chapter);
  Future<bool> hasChapterCached(String bibleId, String chapterId);
}

class ReaderLocalDataSourceImpl implements ReaderLocalDataSource {
  final VerseDao _verseDao;
  final ChapterDao _chapterDao;

  const ReaderLocalDataSourceImpl(this._verseDao, this._chapterDao);

  @override
  Future<List<VerseEntity>> getCachedVerses(
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
  Future<ChapterEntity?> getCachedChapter(
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
  Future<void> cacheVerses(List<VerseEntity> verses) async {
    try {
      await _verseDao.insertVerses(verses);
    } catch (e) {
      throw CacheException('Failed to cache verses: $e');
    }
  }

  @override
  Future<void> cacheChapter(ChapterEntity chapter) async {
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
