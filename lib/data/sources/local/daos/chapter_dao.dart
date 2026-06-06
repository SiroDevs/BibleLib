import 'package:froom/froom.dart';

import '../../../entities/chapter_entity.dart';

@dao
abstract class ChapterDao {
  @Query(
    'SELECT * FROM chapters WHERE bibleId = :bibleId AND bookId = :bookId ORDER BY CAST(number AS INTEGER)',
  )
  Future<List<ChapterEntity>> getChaptersByBook(
    String bibleId,
    String bookId,
  );

  @Query('SELECT * FROM chapters WHERE id = :chapterId AND bibleId = :bibleId')
  Future<ChapterEntity?> getChapter(String chapterId, String bibleId);

  @Insert(onConflict: OnConflictStrategy.replace)
  Future<void> insertChapters(List<ChapterEntity> chapters);

  @Query('DELETE FROM chapters WHERE bibleId = :bibleId')
  Future<void> deleteChaptersByBible(String bibleId);
}
