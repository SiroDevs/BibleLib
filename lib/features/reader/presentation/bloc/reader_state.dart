// lib/features/reader/presentation/bloc/reader_state.dart

import 'package:biblelib/features/reader/domain/entities/chapter_entity.dart';
import 'package:biblelib/features/reader/domain/entities/verse_entity.dart';
import 'package:equatable/equatable.dart';

abstract class ReaderState extends Equatable {
  const ReaderState();
  @override
  List<Object?> get props => [];
}

class ReaderInitial extends ReaderState {
  const ReaderInitial();
}

class ReaderLoading extends ReaderState {
  final String? chapterId;
  const ReaderLoading({this.chapterId});
  @override
  List<Object?> get props => [chapterId];
}

class ReaderLoaded extends ReaderState {
  final String bibleId;
  final ChapterEntity chapter;
  final List<VerseEntity> verses;
  final double fontSize;

  const ReaderLoaded({
    required this.bibleId,
    required this.chapter,
    required this.verses,
    required this.fontSize,
  });

  ReaderLoaded copyWith({
    ChapterEntity? chapter,
    List<VerseEntity>? verses,
    double? fontSize,
  }) =>
      ReaderLoaded(
        bibleId: bibleId,
        chapter: chapter ?? this.chapter,
        verses: verses ?? this.verses,
        fontSize: fontSize ?? this.fontSize,
      );

  @override
  List<Object?> get props => [bibleId, chapter, verses, fontSize];
}

class ReaderError extends ReaderState {
  final String message;
  const ReaderError(this.message);
  @override
  List<Object?> get props => [message];
}
