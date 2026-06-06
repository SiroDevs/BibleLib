import 'package:equatable/equatable.dart';

abstract class SelectionEvent extends Equatable {
  const SelectionEvent();
  @override
  List<Object?> get props => [];
}

class LoadAvailableBiblesEvent extends SelectionEvent {
  const LoadAvailableBiblesEvent();
}

class ToggleBibleSelectionEvent extends SelectionEvent {
  final String bibleId;
  const ToggleBibleSelectionEvent(this.bibleId);
  @override
  List<Object?> get props => [bibleId];
}

class ConfirmSelectionEvent extends SelectionEvent {
  const ConfirmSelectionEvent();
}
