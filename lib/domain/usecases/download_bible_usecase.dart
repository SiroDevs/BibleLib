import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../repos/selection_repo.dart';

class DownloadBibleUseCase {
  final SelectionRepo _repository;
  const DownloadBibleUseCase(this._repository);

  Future<Either<Failure, void>> call(String bibleId) =>
      _repository.downloadBible(bibleId);

  Future<Either<Failure, void>> selectBible(String bibleId) =>
      _repository.selectBible(bibleId);

  Future<Either<Failure, void>> unselectBible(String bibleId) =>
      _repository.unselectBible(bibleId);

  Future<void> saveSelections(List<String> bibleIds) =>
      _repository.saveSelectedBibleIds(bibleIds);

  Future<void> setActive(String bibleId) =>
      _repository.setActiveBibleId(bibleId);

  Future<void> markFirstLaunchDone() => _repository.setFirstLaunchDone();
}
