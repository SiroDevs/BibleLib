// lib/features/selection/domain/repositories/selection_repository.dart

import 'package:biblelib/core/errors/failures.dart';
import 'package:biblelib/features/selection/domain/entities/bible_entity.dart';
import 'package:dartz/dartz.dart';

abstract class SelectionRepository {
  Future<Either<Failure, List<BibleEntity>>> getAvailableBibles();
  Future<Either<Failure, List<BibleEntity>>> getDownloadedBibles();
  Future<Either<Failure, void>> downloadBible(String bibleId);
  Future<bool> isFirstLaunch();
  Future<void> setFirstLaunchDone();
  Future<void> saveSelectedBibleIds(List<String> ids);
  Future<List<String>> getSelectedBibleIds();
  Future<void> setActiveBibleId(String id);
  Future<String?> getActiveBibleId();
}
