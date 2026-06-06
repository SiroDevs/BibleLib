// lib/features/reader/domain/usecases/get_next_chapter_usecase.dart

import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../entities/chapter_entity.dart';
import '../repositories/reader_repository.dart';

class GetNextChapterUseCase {
  final ReaderRepository _repository;
  const GetNextChapterUseCase(this._repository);

  Future<Either<Failure, ChapterEntity>> call(
    String bibleId,
    String chapterId,
  ) =>
      _repository.getChapter(bibleId, chapterId);
}
