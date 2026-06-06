import 'package:froom/froom.dart';

import '../entities/bible_entity.dart';

@dao
abstract class BibleDao {
  @Query('SELECT * FROM bibles')
  Future<List<BibleEntity>> getAllBibles();

  @Query('SELECT * FROM bibles WHERE isDownloaded = 1')
  Future<List<BibleEntity>> getDownloadedBibles();

  @Query('SELECT * FROM bibles WHERE isSelected = 1')
  Future<List<BibleEntity>> getSelectedBibles();

  @Query('SELECT * FROM bibles WHERE id = :id')
  Future<BibleEntity?> getBibleById(String id);

  @Insert(onConflict: OnConflictStrategy.replace)
  Future<void> insertBible(BibleEntity bible);

  @Insert(onConflict: OnConflictStrategy.replace)
  Future<void> insertBibles(List<BibleEntity> bibles);

  @Query('UPDATE bibles SET isDownloaded = 1 WHERE id = :id')
  Future<void> markAsDownloaded(String id);

  @Query('UPDATE bibles SET isSelected = 1 WHERE id = :id')
  Future<void> markAsSelected(String id);

  @Query('UPDATE bibles SET isSelected = 0 WHERE id = :id')
  Future<void> markAsUnselected(String id);

  @Query('DELETE FROM bibles WHERE id = :id')
  Future<void> deleteBible(String id);
}
