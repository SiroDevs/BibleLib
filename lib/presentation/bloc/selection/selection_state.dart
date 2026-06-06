import 'package:equatable/equatable.dart';

import '../../../domain/entities/bible_entity.dart';

abstract class SelectionState extends Equatable {
  const SelectionState();
  @override
  List<Object?> get props => [];
}

class SelectionInitial extends SelectionState {
  const SelectionInitial();
}

class SelectionLoading extends SelectionState {
  const SelectionLoading();
}

class SelectionLoaded extends SelectionState {
  final List<BibleEntity> bibles;
  final Set<String> selectedIds;

  const SelectionLoaded({
    required this.bibles,
    this.selectedIds = const {},
  });

  SelectionLoaded copyWith({
    List<BibleEntity>? bibles,
    Set<String>? selectedIds,
  }) =>
      SelectionLoaded(
        bibles: bibles ?? this.bibles,
        selectedIds: selectedIds ?? this.selectedIds,
      );

  bool get hasSelection => selectedIds.isNotEmpty;

  @override
  List<Object?> get props => [bibles, selectedIds];
}

class SelectionDownloading extends SelectionState {
  final int completed;
  final int total;
  final String currentBibleName;

  const SelectionDownloading({
    required this.completed,
    required this.total,
    required this.currentBibleName,
  });

  double get progress => total == 0 ? 0 : completed / total;

  @override
  List<Object?> get props => [completed, total, currentBibleName];
}

class SelectionDone extends SelectionState {
  const SelectionDone();
}

class SelectionError extends SelectionState {
  final String message;
  const SelectionError(this.message);
  @override
  List<Object?> get props => [message];
}
