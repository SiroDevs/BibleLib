// lib/features/reader/domain/usecases/get_next_chapter_usecase.dart

import 'package:biblelib/core/errors/failures.dart';
import 'package:biblelib/features/reader/domain/entities/chapter_entity.dart';
import 'package:biblelib/features/reader/domain/repositories/reader_repository.dart';
import 'package:dartz/dartz.dart';

class GetNextChapterUseCase {
  final ReaderRepository _repository;
  const GetNextChapterUseCase(this._repository);

  Future<Either<Failure, ChapterEntity>> call(
    String bibleId,
    String chapterId,
  ) =>
      _repository.getChapter(bibleId, chapterId);
}
