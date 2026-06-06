import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../../data/local/entities/bible_entity.dart';

abstract class SelectionRepo {
  Future<Either<Failure, List<BibleEntity>>> getAvailableBibles();
  Future<Either<Failure, List<BibleEntity>>> getDownloadedBibles();
  Future<Either<Failure, List<BibleEntity>>> getSelectedBibles();
  Future<Either<Failure, void>> downloadBible(String bibleId);
  Future<Either<Failure, void>> selectBible(String bibleId);
  Future<Either<Failure, void>> unselectBible(String bibleId);
  Future<bool> isFirstLaunch();
  Future<void> setFirstLaunchDone();
  Future<void> saveSelectedBibleIds(List<String> ids);
  Future<List<String>> getSelectedBibleIds();
  Future<void> setActiveBibleId(String id);
  Future<String?> getActiveBibleId();
}
