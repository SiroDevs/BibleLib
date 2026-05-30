// lib/features/reader/domain/repositories/reader_repository.dart

import 'package:biblelib/core/errors/failures.dart';
import 'package:biblelib/features/reader/domain/entities/chapter_entity.dart';
import 'package:biblelib/features/reader/domain/entities/verse_entity.dart';
import 'package:dartz/dartz.dart';

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
