// lib/features/reader/domain/usecases/get_chapter_verses_usecase.dart

import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../entities/verse_entity.dart';
import '../repositories/reader_repository.dart';

class GetChapterVersesUseCase {
  final ReaderRepository _repository;
  const GetChapterVersesUseCase(this._repository);

  Future<Either<Failure, List<VerseEntity>>> call(
    String bibleId,
    String chapterId,
  ) =>
      _repository.getChapterVerses(bibleId, chapterId);
}
