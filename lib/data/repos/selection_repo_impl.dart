import 'package:dartz/dartz.dart';

import '../../core/errors/exceptions.dart';
import '../../core/errors/failures.dart';
import '../../domain/models/bible_model.dart';
import '../../domain/repos/selection_repo.dart';
import '../sources/local/selection_local_datasource.dart';
import '../sources/remote/selection_remote_datasource.dart';

class SelectionRepoImpl implements SelectionRepo {
  final SelectionRemoteDataSource _remote;
  final SelectionLocalDataSource _local;

  const SelectionRepoImpl(this._remote, this._local);

  @override
  Future<Either<Failure, List<BibleModel>>> getAvailableBibles() async {
    try {
      final cached = await _local.getCachedBibles();
      if (cached.isNotEmpty) {
        return Right(cached.map((m) => m.toModel()).toList());
      }
      final remote = await _remote.getAvailableBibles();
      await _local.cacheBibles(remote);
      return Right(remote.map((m) => m.toModel()).toList());
    } on NetworkException catch (e) {
      return Left(NetworkFailure(e.message));
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    } on UnauthorizedException catch (e) {
      return Left(UnauthorizedFailure(e.message));
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, List<BibleModel>>> getDownloadedBibles() async {
    try {
      final models = await _local.getDownloadedBibles();
      return Right(models.map((m) => m.toModel()).toList());
    } on CacheException catch (e) {
      return Left(CacheFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, List<BibleModel>>> getSelectedBibles() async {
    try {
      final models = await _local.getSelectedBibles();
      return Right(models.map((m) => m.toModel()).toList());
    } on CacheException catch (e) {
      return Left(CacheFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> downloadBible(String bibleId) async {
    try {
      await _local.markBibleAsDownloaded(bibleId);
      return const Right(null);
    } on CacheException catch (e) {
      return Left(CacheFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> selectBible(String bibleId) async {
    try {
      await _local.markBibleAsSelected(bibleId);
      return const Right(null);
    } on CacheException catch (e) {
      return Left(CacheFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, void>> unselectBible(String bibleId) async {
    try {
      await _local.markBibleAsUnselected(bibleId);
      return const Right(null);
    } on CacheException catch (e) {
      return Left(CacheFailure(e.message));
    }
  }

  @override
  Future<bool> isFirstLaunch() => _local.isFirstLaunch();

  @override
  Future<void> setFirstLaunchDone() => _local.setFirstLaunchDone();

  @override
  Future<void> saveSelectedBibleIds(List<String> ids) =>
      _local.saveSelectedBibleIds(ids);

  @override
  Future<List<String>> getSelectedBibleIds() => _local.getSelectedBibleIds();

  @override
  Future<void> setActiveBibleId(String id) => _local.setActiveBibleId(id);

  @override
  Future<String?> getActiveBibleId() => _local.getActiveBibleId();
}
