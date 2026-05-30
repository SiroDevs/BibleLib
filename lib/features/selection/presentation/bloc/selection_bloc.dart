// lib/features/selection/presentation/bloc/selection_bloc.dart

import 'package:biblelib/core/network/api_client.dart';
import 'package:biblelib/core/network/local/app_database.dart';
import 'package:biblelib/core/utils/bible_download_worker.dart';
import 'package:biblelib/features/selection/domain/usecases/download_bible_usecase.dart';
import 'package:biblelib/features/selection/domain/usecases/get_available_bibles_usecase.dart';
import 'package:biblelib/features/selection/presentation/bloc/selection_event.dart';
import 'package:biblelib/features/selection/presentation/bloc/selection_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SelectionBloc extends Bloc<SelectionEvent, SelectionState> {
  final GetAvailableBiblesUseCase _getAvailableBibles;
  final DownloadBibleUseCase _downloadBible;
  final SharedPreferences _prefs;

  SelectionBloc({
    required GetAvailableBiblesUseCase getAvailableBibles,
    required DownloadBibleUseCase downloadBible,
    required SharedPreferences prefs,
  })  : _getAvailableBibles = getAvailableBibles,
        _downloadBible = downloadBible,
        _prefs = prefs,
        super(const SelectionInitial()) {
    on<LoadAvailableBiblesEvent>(_onLoad);
    on<ToggleBibleSelectionEvent>(_onToggle);
    on<ConfirmSelectionEvent>(_onConfirm);
  }

  Future<void> _onLoad(
    LoadAvailableBiblesEvent event,
    Emitter<SelectionState> emit,
  ) async {
    emit(const SelectionLoading());
    final result = await _getAvailableBibles();
    result.fold(
      (failure) => emit(SelectionError(failure.message)),
      (bibles) => emit(SelectionLoaded(bibles: bibles)),
    );
  }

  void _onToggle(
    ToggleBibleSelectionEvent event,
    Emitter<SelectionState> emit,
  ) {
    if (state is! SelectionLoaded) return;
    final current = state as SelectionLoaded;
    final updated = Set<String>.from(current.selectedIds);
    if (updated.contains(event.bibleId)) {
      updated.remove(event.bibleId);
    } else {
      updated.add(event.bibleId);
    }
    emit(current.copyWith(selectedIds: updated));
  }

  Future<void> _onConfirm(
    ConfirmSelectionEvent event,
    Emitter<SelectionState> emit,
  ) async {
    if (state is! SelectionLoaded) return;
    final loaded = state as SelectionLoaded;
    if (!loaded.hasSelection) return;

    final selectedIds = loaded.selectedIds.toList();
    final selectedBibles = loaded.bibles
        .where((b) => loaded.selectedIds.contains(b.id))
        .toList();

    // ── Phase 1: foreground – mark bibles as selected & show progress ──────
    for (int i = 0; i < selectedBibles.length; i++) {
      emit(SelectionDownloading(
        completed: i,
        total: selectedBibles.length,
        currentBibleName: selectedBibles[i].name,
      ));
      await _downloadBible(selectedBibles[i].id);
    }

    emit(SelectionDownloading(
      completed: selectedBibles.length,
      total: selectedBibles.length,
      currentBibleName: 'Finishing up...',
    ));

    // ── Phase 2: persist selections & set active ────────────────────────────
    await _downloadBible.saveSelections(selectedIds);
    await _downloadBible.setActive(selectedIds.first);
    await _downloadBible.markFirstLaunchDone();

    // ── Phase 3: enqueue WorkManager task for full offline download ─────────
    // Downloads all chapters+verses for every selected Bible in the background.
    // Genesis 1 will be fetched on-demand by the ReaderBloc; the worker fills
    // the rest so subsequent chapters are available offline.
    try {
      await BibleDownloadWorker.enqueue(selectedIds);
    } catch (_) {
      // Non-fatal – user can still read online
    }

    emit(const SelectionDone());
  }
}
