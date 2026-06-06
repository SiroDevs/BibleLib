// lib/features/reader/domain/repositories/reader_repository.dart

import 'package:dartz/dartz.dart';

import '../../core/errors/failures.dart';
import '../entities/chapter_entity.dart';
import '../entities/verse_entity.dart';

abstract class ReaderRepository {
  Future<Either<Failure, List<VerseEntity>>> getChapterVerses(
    String bibleId,
    String chapterId,
  );

  Future<Either<Failure, ChapterEntity>> getChapter(
    String bibleId,
    String chapterId,
  );
}
