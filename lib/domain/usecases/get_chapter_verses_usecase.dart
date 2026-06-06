import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../models/verse_model.dart';
import '../repos/reader_repo.dart';

class GetChapterVersesUseCase {
  final ReaderRepo _repository;
  const GetChapterVersesUseCase(this._repository);

  Future<Either<Failure, List<VerseModel>>> call(
    String bibleId,
    String chapterId,
  ) =>
      _repository.getChapterVerses(bibleId, chapterId);
}
