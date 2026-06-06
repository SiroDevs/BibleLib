import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';

abstract class SettingsEvent extends Equatable {
  const SettingsEvent();
  @override
  List<Object?> get props => [];
}

class LoadSettingsEvent extends SettingsEvent {
  const LoadSettingsEvent();
}

class ChangeThemeModeEvent extends SettingsEvent {
  final ThemeMode mode;
  const ChangeThemeModeEvent(this.mode);
  @override
  List<Object?> get props => [mode];
}

class ChangeFontSizeEvent extends SettingsEvent {
  final double fontSize;
  const ChangeFontSizeEvent(this.fontSize);
  @override
  List<Object?> get props => [fontSize];
}
