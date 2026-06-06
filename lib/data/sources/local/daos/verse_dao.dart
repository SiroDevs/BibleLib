import 'package:froom/froom.dart';

import '../../../entities/verse_entity.dart';

@dao
abstract class VerseDao {
  @Query(
    'SELECT * FROM verses WHERE bibleId = :bibleId AND chapterId = :chapterId ORDER BY verseNumber',
  )
  Future<List<VerseEntity>> getVersesByChapter(
    String bibleId,
    String chapterId,
  );

  @Query(
    'SELECT COUNT(*) FROM verses WHERE bibleId = :bibleId AND chapterId = :chapterId',
  )
  Future<int?> countVersesInChapter(String bibleId, String chapterId);

  @Insert(onConflict: OnConflictStrategy.replace)
  Future<void> insertVerses(List<VerseEntity> verses);

  @Query('DELETE FROM verses WHERE bibleId = :bibleId')
  Future<void> deleteVersesByBible(String bibleId);

  @Query('SELECT DISTINCT chapterId FROM verses WHERE bibleId = :bibleId')
  Future<List<String>> getDownloadedChapterIds(String bibleId);
}
