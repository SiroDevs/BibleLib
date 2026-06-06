import 'package:equatable/equatable.dart';

import '../../../domain/models/chapter_model.dart';
import '../../../domain/models/verse_model.dart';

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
  final ChapterModel chapter;
  final List<VerseModel> verses;
  final double fontSize;

  const ReaderLoaded({
    required this.bibleId,
    required this.chapter,
    required this.verses,
    required this.fontSize,
  });

  ReaderLoaded copyWith({
    ChapterModel? chapter,
    List<VerseModel>? verses,
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
