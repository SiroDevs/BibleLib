import 'package:froom/froom.dart';

import '../../../../data/models/bible_model.dart';

@dao
abstract class BibleDao {
  @Query('SELECT * FROM bibles')
  Future<List<BibleModel>> getAllBibles();

  @Query('SELECT * FROM bibles WHERE isDownloaded = 1')
  Future<List<BibleModel>> getDownloadedBibles();

  @Query('SELECT * FROM bibles WHERE isSelected = 1')
  Future<List<BibleModel>> getSelectedBibles();

  @Query('SELECT * FROM bibles WHERE id = :id')
  Future<BibleModel?> getBibleById(String id);

  @Insert(onConflict: OnConflictStrategy.replace)
  Future<void> insertBible(BibleModel bible);

  @Insert(onConflict: OnConflictStrategy.replace)
  Future<void> insertBibles(List<BibleModel> bibles);

  @Query('UPDATE bibles SET isDownloaded = 1 WHERE id = :id')
  Future<void> markAsDownloaded(String id);

  @Query('UPDATE bibles SET isSelected = 1 WHERE id = :id')
  Future<void> markAsSelected(String id);

  @Query('UPDATE bibles SET isSelected = 0 WHERE id = :id')
  Future<void> markAsUnselected(String id);

  @Query('DELETE FROM bibles WHERE id = :id')
  Future<void> deleteBible(String id);
}
