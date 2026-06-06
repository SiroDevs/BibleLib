import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../models/chapter_model.dart';
import '../models/verse_model.dart';

abstract class ReaderRepo {
  Future<Either<Failure, List<VerseModel>>> getChapterVerses(
    String bibleId,
    String chapterId,
  );

  Future<Either<Failure, ChapterModel>> getChapter(
    String bibleId,
    String chapterId,
  );
}
