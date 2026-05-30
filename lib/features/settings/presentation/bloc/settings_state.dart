// lib/features/settings/presentation/bloc/settings_state.dart

import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';

class SettingsState extends Equatable {
  final ThemeMode themeMode;
  final double fontSize;

  const SettingsState({
    this.themeMode = ThemeMode.system,
    this.fontSize = 16.0,
  });

  SettingsState copyWith({ThemeMode? themeMode, double? fontSize}) =>
      SettingsState(
        themeMode: themeMode ?? this.themeMode,
        fontSize: fontSize ?? this.fontSize,
      );

  @override
  List<Object?> get props => [themeMode, fontSize];
}
