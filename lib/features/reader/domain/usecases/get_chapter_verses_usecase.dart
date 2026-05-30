// lib/features/reader/domain/usecases/get_chapter_verses_usecase.dart

import 'package:biblelib/core/errors/failures.dart';
import 'package:biblelib/features/reader/domain/entities/verse_entity.dart';
import 'package:biblelib/features/reader/domain/repositories/reader_repository.dart';
import 'package:dartz/dartz.dart';

class GetChapterVersesUseCase {
  final ReaderRepository _repository;
  const GetChapterVersesUseCase(this._repository);

  Future<Either<Failure, List<VerseEntity>>> call(
    String bibleId,
    String chapterId,
  ) =>
      _repository.getChapterVerses(bibleId, chapterId);
}
