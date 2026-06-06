import 'package:dartz/dartz.dart';

import '../../core/errors/exceptions.dart';
import '../../core/errors/failures.dart';
import '../../domain/models/chapter_model.dart';
import '../../domain/models/verse_model.dart';
import '../../domain/repos/reader_repo.dart';
import '../sources/local/reader_local_datasource.dart';
import '../sources/remote/reader_remote_datasource.dart';


class ReaderRepoImpl implements ReaderRepo {
  final ReaderRemoteDataSource _remote;
  final ReaderLocalDataSource _local;

  const ReaderRepoImpl(this._remote, this._local);

  @override
  Future<Either<Failure, List<VerseModel>>> getChapterVerses(
    String bibleId,
    String chapterId,
  ) async {
    try {
      final cached = await _local.getCachedVerses(bibleId, chapterId);
      if (cached.isNotEmpty) {
        return Right(cached.map((m) => m.toModel()).toList());
      }
      final remote = await _remote.getChapterVerses(bibleId, chapterId);
      await _local.cacheVerses(remote);
      return Right(remote.map((m) => m.toModel()).toList());
    } on NetworkException catch (e) {
      // Try cache fallback on network failure
      try {
        final fallback = await _local.getCachedVerses(bibleId, chapterId);
        if (fallback.isNotEmpty) {
          return Right(fallback.map((m) => m.toModel()).toList());
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
  Future<Either<Failure, ChapterModel>> getChapter(
    String bibleId,
    String chapterId,
  ) async {
    try {
      final cached = await _local.getCachedChapter(bibleId, chapterId);
      if (cached != null) {
        return Right(cached.toModel());
      }
      final remote = await _remote.getChapter(bibleId, chapterId);
      await _local.cacheChapter(remote);
      return Right(remote.toModel());
    } on NetworkException catch (e) {
      return Left(NetworkFailure(e.message));
    } on ServerException catch (e) {
      return Left(ServerFailure(e.message));
    }
  }
}
