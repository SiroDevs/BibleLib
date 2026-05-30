// lib/features/reader/presentation/bloc/reader_event.dart

import 'package:equatable/equatable.dart';

abstract class ReaderEvent extends Equatable {
  const ReaderEvent();
  @override
  List<Object?> get props => [];
}

class LoadChapterEvent extends ReaderEvent {
  final String bibleId;
  final String chapterId;
  const LoadChapterEvent({required this.bibleId, required this.chapterId});
  @override
  List<Object?> get props => [bibleId, chapterId];
}

class NavigateNextChapterEvent extends ReaderEvent {
  const NavigateNextChapterEvent();
}

class NavigatePreviousChapterEvent extends ReaderEvent {
  const NavigatePreviousChapterEvent();
}

class UpdateFontSizeEvent extends ReaderEvent {
  final double fontSize;
  const UpdateFontSizeEvent(this.fontSize);
  @override
  List<Object?> get props => [fontSize];
}

class InitReaderEvent extends ReaderEvent {
  const InitReaderEvent();
}
