// lib/core/network/local/daos/chapter_dao.dart

import 'package:biblelib/features/reader/data/models/chapter_model.dart';
import 'package:floor/floor.dart';

@dao
abstract class ChapterDao {
  @Query(
    'SELECT * FROM chapters WHERE bibleId = :bibleId AND bookId = :bookId ORDER BY CAST(number AS INTEGER)',
  )
  Future<List<ChapterModel>> getChaptersByBook(
    String bibleId,
    String bookId,
  );

  @Query('SELECT * FROM chapters WHERE id = :chapterId AND bibleId = :bibleId')
  Future<ChapterModel?> getChapter(String chapterId, String bibleId);

  @Insert(onConflict: OnConflictStrategy.replace)
  Future<void> insertChapters(List<ChapterModel> chapters);

  @Query('DELETE FROM chapters WHERE bibleId = :bibleId')
  Future<void> deleteChaptersByBible(String bibleId);
}
