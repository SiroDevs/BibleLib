// lib/features/reader/data/repositories/reader_repository_impl.dart

import 'package:dartz/dartz.dart';

import '../../core/errors/exceptions.dart';
import '../../core/errors/failures.dart';
import '../datasources/reader_local_datasource.dart';
import '../datasources/reader_remote_datasource.dart';
import '../../domain/entities/chapter_entity.dart';
import '../../domain/entities/verse_entity.dart';
import '../../domain/repositories/reader_repository.dart';

class ReaderRepositoryImpl implements ReaderRepository {
  final ReaderRemoteDataSource _remote;
  final ReaderLocalDataSource _local;

  const ReaderRepositoryImpl(this._remote, this._local);

  @override
  Future<Either<Failure, List<VerseEntity>>> getChapterVerses(
    String bibleId,
    String chapterId,
  ) async {
    try {
      final cached = await _local.getCachedVerses(bibleId, chapterId);
      if (cached.isNotEmpty) {
        return Right(cached.map((m) => m.toEntity()).toList());
      }
      final remote = await _remote.getChapterVerses(bibleId, chapterId);
      await _local.cacheVerses(remote);
      return Right(remote.map((m) => m.toEntity()).toList());
    } on NetworkException catch (e) {
      // Try cache fallback on network failure
      try {
        final fallback = await _local.getCachedVerses(bibleId, chapterId);
        if (fallback.isNotEmpty) {
          return Right(fallback.map((m) => m.toEntity()).toList());
        }
      } catch (_) {}
      return Left(NetworkFailure(e.message));
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    } on CacheException catch (e) {
      return Left(CacheFailure(e.message));
    }
  }

  @override
  Future<Either<Failure, ChapterEntity>> getChapter(
    String bibleId,
    String chapterId,
  ) async {
    try {
      final cached = await _local.getCachedChapter(bibleId, chapterId);
      if (cached != null) {
        return Right(cached.toEntity());
      }
      final remote = await _remote.getChapter(bibleId, chapterId);
      await _local.cacheChapter(remote);
      return Right(remote.toEntity());
    } on NetworkException catch (e) {
      return Left(NetworkFailure(e.message));
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }
}
