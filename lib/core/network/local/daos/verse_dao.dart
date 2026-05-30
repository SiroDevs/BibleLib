// lib/core/network/local/daos/verse_dao.dart

import 'package:biblelib/features/reader/data/models/verse_model.dart';
import 'package:floor/floor.dart';

@dao
abstract class VerseDao {
  @Query(
    'SELECT * FROM verses WHERE bibleId = :bibleId AND chapterId = :chapterId ORDER BY verseNumber',
  )
  Future<List<VerseModel>> getVersesByChapter(
    String bibleId,
    String chapterId,
  );

  @Query(
    'SELECT COUNT(*) FROM verses WHERE bibleId = :bibleId AND chapterId = :chapterId',
  )
  Future<int?> countVersesInChapter(String bibleId, String chapterId);

  @Insert(onConflict: OnConflictStrategy.replace)
  Future<void> insertVerses(List<VerseModel> verses);

  @Query('DELETE FROM verses WHERE bibleId = :bibleId')
  Future<void> deleteVersesByBible(String bibleId);

  @Query('SELECT DISTINCT chapterId FROM verses WHERE bibleId = :bibleId')
  Future<List<String>> getDownloadedChapterIds(String bibleId);
}
