// lib/features/selection/data/repositories/selection_repository_impl.dart

import 'package:biblelib/core/errors/exceptions.dart';
import 'package:biblelib/core/errors/failures.dart';
import 'package:biblelib/features/selection/data/datasources/selection_local_datasource.dart';
import 'package:biblelib/features/selection/data/datasources/selection_remote_datasource.dart';
import 'package:biblelib/features/selection/domain/entities/bible_entity.dart';
import 'package:biblelib/features/selection/domain/repositories/selection_repository.dart';
import 'package:dartz/dartz.dart';

class SelectionRepositoryImpl implements SelectionRepository {
  final SelectionRemoteDataSource _remote;
  final SelectionLocalDataSource _local;

  const SelectionRepositoryImpl(this._remote, this._local);

  @override
  Future<Either<Failure, List<BibleEntity>>> getAvailableBibles() async {
    try {
      final cached = await _local.getCachedBibles();
      if (cached.isNotEmpty) {
        return Right(cached.map((m) => m.toEntity()).toList());
      }
      final remote = await _remote.getAvailableBibles();
      await _local.cacheBibles(remote);
      return Right(remote.map((m) => m.toEntity()).toList());
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
  Future<Either<Failure, List<BibleEntity>>> getDownloadedBibles() async {
    try {
      final models = await _local.getDownloadedBibles();
      return Right(models.map((m) => m.toEntity()).toList());
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
