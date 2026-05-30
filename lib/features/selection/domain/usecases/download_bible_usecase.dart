// lib/features/selection/domain/usecases/download_bible_usecase.dart

import 'package:biblelib/core/errors/failures.dart';
import 'package:biblelib/features/selection/domain/repositories/selection_repository.dart';
import 'package:dartz/dartz.dart';

class DownloadBibleUseCase {
  final SelectionRepository _repository;
  const DownloadBibleUseCase(this._repository);

  Future<Either<Failure, void>> call(String bibleId) =>
      _repository.downloadBible(bibleId);

  Future<void> saveSelections(List<String> bibleIds) =>
      _repository.saveSelectedBibleIds(bibleIds);

  Future<void> setActive(String bibleId) =>
      _repository.setActiveBibleId(bibleId);

  Future<void> markFirstLaunchDone() => _repository.setFirstLaunchDone();
}
