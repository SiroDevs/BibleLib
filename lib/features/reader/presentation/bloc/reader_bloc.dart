// lib/features/reader/presentation/bloc/reader_bloc.dart

import 'package:biblelib/core/constants/app_constants.dart';
import 'package:biblelib/features/reader/domain/usecases/get_chapter_verses_usecase.dart';
import 'package:biblelib/features/reader/domain/usecases/get_next_chapter_usecase.dart';
import 'package:biblelib/features/reader/presentation/bloc/reader_event.dart';
import 'package:biblelib/features/reader/presentation/bloc/reader_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ReaderBloc extends Bloc<ReaderEvent, ReaderState> {
  final GetChapterVersesUseCase _getChapterVerses;
  final GetNextChapterUseCase _getChapter;
  final SharedPreferences _prefs;

  ReaderBloc({
    required GetChapterVersesUseCase getChapterVerses,
    required GetNextChapterUseCase getNextChapter,
    required SharedPreferences prefs,
  })  : _getChapterVerses = getChapterVerses,
        _getChapter = getNextChapter,
        _prefs = prefs,
        super(const ReaderInitial()) {
    on<InitReaderEvent>(_onInit);
    on<LoadChapterEvent>(_onLoad);
    on<NavigateNextChapterEvent>(_onNext);
    on<NavigatePreviousChapterEvent>(_onPrevious);
    on<UpdateFontSizeEvent>(_onFontSize);
  }

  Future<void> _onInit(
    InitReaderEvent event,
    Emitter<ReaderState> emit,
  ) async {
    final bibleId = _prefs.getString(kActiveBibleIdKey);
    final chapterId = kDefaultChapterId;
    final fontSize = _prefs.getDouble(kFontSizeKey) ?? kDefaultFontSize;

    if (bibleId == null || bibleId.isEmpty) {
      emit(const ReaderError('No active Bible found. Please select a Bible.'));
      return;
    }

    emit(ReaderLoading(chapterId: chapterId));
    await _loadChapter(bibleId, chapterId, fontSize, emit);
  }

  Future<void> _onLoad(
    LoadChapterEvent event,
    Emitter<ReaderState> emit,
  ) async {
    final fontSize = state is ReaderLoaded
        ? (state as ReaderLoaded).fontSize
        : _prefs.getDouble(kFontSizeKey) ?? kDefaultFontSize;

    emit(ReaderLoading(chapterId: event.chapterId));
    await _loadChapter(event.bibleId, event.chapterId, fontSize, emit);
  }

  Future<void> _onNext(
    NavigateNextChapterEvent event,
    Emitter<ReaderState> emit,
  ) async {
    if (state is! ReaderLoaded) return;
    final current = state as ReaderLoaded;
    if (!current.chapter.hasNext) return;

    emit(ReaderLoading(chapterId: current.chapter.nextId));
    await _loadChapter(
      current.bibleId,
      current.chapter.nextId!,
      current.fontSize,
      emit,
    );
  }

  Future<void> _onPrevious(
    NavigatePreviousChapterEvent event,
    Emitter<ReaderState> emit,
  ) async {
    if (state is! ReaderLoaded) return;
    final current = state as ReaderLoaded;
    if (!current.chapter.hasPrevious) return;

    emit(ReaderLoading(chapterId: current.chapter.previousId));
    await _loadChapter(
      current.bibleId,
      current.chapter.previousId!,
      current.fontSize,
      emit,
    );
  }

  void _onFontSize(
    UpdateFontSizeEvent event,
    Emitter<ReaderState> emit,
  ) {
    if (state is! ReaderLoaded) return;
    final clamped = event.fontSize
        .clamp(kMinFontSize, kMaxFontSize)
        .toDouble();
    _prefs.setDouble(kFontSizeKey, clamped);
    emit((state as ReaderLoaded).copyWith(fontSize: clamped));
  }

  Future<void> _loadChapter(
    String bibleId,
    String chapterId,
    double fontSize,
    Emitter<ReaderState> emit,
  ) async {
    final chapterResult = await _getChapter(bibleId, chapterId);
    final versesResult = await _getChapterVerses(bibleId, chapterId);

    chapterResult.fold(
      (failure) => emit(ReaderError(failure.message)),
      (chapter) {
        versesResult.fold(
          (failure) => emit(ReaderError(failure.message)),
          (verses) => emit(
            ReaderLoaded(
              bibleId: bibleId,
              chapter: chapter,
              verses: verses,
              fontSize: fontSize,
            ),
          ),
        );
      },
    );
  }
}
