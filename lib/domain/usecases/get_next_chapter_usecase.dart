import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../models/chapter_model.dart';
import '../repos/reader_repo.dart';

class GetNextChapterUseCase {
  final ReaderRepo _repository;
  const GetNextChapterUseCase(this._repository);

  Future<Either<Failure, ChapterModel>> call(
    String bibleId,
    String chapterId,
  ) =>
      _repository.getChapter(bibleId, chapterId);
}
