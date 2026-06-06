import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../core/constants/app_constants.dart';
import 'settings_event.dart';
import 'settings_state.dart';

class SettingsBloc extends Bloc<SettingsEvent, SettingsState> {
  final SharedPreferences _prefs;

  SettingsBloc({required SharedPreferences prefs})
      : _prefs = prefs,
        super(const SettingsState()) {
    on<LoadSettingsEvent>(_onLoad);
    on<ChangeThemeModeEvent>(_onTheme);
    on<ChangeFontSizeEvent>(_onFontSize);
  }

  void _onLoad(LoadSettingsEvent event, Emitter<SettingsState> emit) {
    final themePref = _prefs.getString(kThemeModeKey) ?? 'system';
    final fontSize = _prefs.getDouble(kFontSizeKey) ?? kDefaultFontSize_;
    final themeMode = switch (themePref) {
      'light' => ThemeMode.light,
      'dark' => ThemeMode.dark,
      _ => ThemeMode.system,
    };
    emit(state.copyWith(themeMode: themeMode, fontSize: fontSize));
  }

  Future<void> _onTheme(
    ChangeThemeModeEvent event,
    Emitter<SettingsState> emit,
  ) async {
    final val = switch (event.mode) {
      ThemeMode.light => 'light',
      ThemeMode.dark => 'dark',
      ThemeMode.system => 'system',
    };
    await _prefs.setString(kThemeModeKey, val);
    emit(state.copyWith(themeMode: event.mode));
  }

  Future<void> _onFontSize(
    ChangeFontSizeEvent event,
    Emitter<SettingsState> emit,
  ) async {
    final clamped = event.fontSize.clamp(kMinFontSize, kMaxFontSize);
    await _prefs.setDouble(kFontSizeKey, clamped);
    emit(state.copyWith(fontSize: clamped));
  }
}
