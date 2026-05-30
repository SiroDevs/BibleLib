// lib/features/settings/presentation/pages/settings_screen.dart

import 'package:biblelib/core/constants/app_constants.dart';
import 'package:biblelib/core/di/service_locator.dart';
import 'package:biblelib/core/theme/app_colors.dart';
import 'package:biblelib/features/settings/presentation/bloc/settings_bloc.dart';
import 'package:biblelib/features/settings/presentation/bloc/settings_event.dart';
import 'package:biblelib/features/settings/presentation/bloc/settings_state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => sl<SettingsBloc>()..add(const LoadSettingsEvent()),
      child: const _SettingsView(),
    );
  }
}

class _SettingsView extends StatelessWidget {
  const _SettingsView();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: BlocBuilder<SettingsBloc, SettingsState>(
        builder: (context, state) {
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _SectionHeader(title: 'Appearance'),
              _ThemeSelector(current: state.themeMode),
              const SizedBox(height: 24),
              _SectionHeader(title: 'Reading'),
              _FontSizeSelector(fontSize: state.fontSize),
              const SizedBox(height: 24),
              _SectionHeader(title: 'About'),
              const _AboutTile(),
            ],
          );
        },
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;
  const _SectionHeader({required this.title});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Text(
        title.toUpperCase(),
        style: Theme.of(context).textTheme.labelLarge?.copyWith(
          color: AppColors.primary,
          letterSpacing: 1.2,
          fontSize: 11,
        ),
      ),
    );
  }
}

class _ThemeSelector extends StatelessWidget {
  final ThemeMode current;
  const _ThemeSelector({required this.current});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.zero,
      child: Column(
        children: [
          _ThemeOption(
            label: 'System Default',
            icon: Icons.brightness_auto_rounded,
            mode: ThemeMode.system,
            selected: current == ThemeMode.system,
          ),
          const Divider(height: 1, indent: 16, endIndent: 16),
          _ThemeOption(
            label: 'Light',
            icon: Icons.light_mode_rounded,
            mode: ThemeMode.light,
            selected: current == ThemeMode.light,
          ),
          const Divider(height: 1, indent: 16, endIndent: 16),
          _ThemeOption(
            label: 'Dark',
            icon: Icons.dark_mode_rounded,
            mode: ThemeMode.dark,
            selected: current == ThemeMode.dark,
          ),
        ],
      ),
    );
  }
}

class _ThemeOption extends StatelessWidget {
  final String label;
  final IconData icon;
  final ThemeMode mode;
  final bool selected;

  const _ThemeOption({
    required this.label,
    required this.icon,
    required this.mode,
    required this.selected,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon,
          color: selected ? AppColors.primary : AppColors.textSecondary),
      title: Text(label),
      trailing: selected
          ? const Icon(Icons.check_rounded, color: AppColors.primary)
          : null,
      onTap: () =>
          context.read<SettingsBloc>().add(ChangeThemeModeEvent(mode)),
    );
  }
}

class _FontSizeSelector extends StatelessWidget {
  final double fontSize;
  const _FontSizeSelector({required this.fontSize});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Font Size', style: Theme.of(context).textTheme.titleMedium),
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    '${fontSize.toStringAsFixed(0)}pt',
                    style: const TextStyle(
                      color: AppColors.primary,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              'Preview text at current size',
              style: TextStyle(fontSize: fontSize, height: 1.5),
            ),
            Slider(
              value: fontSize,
              min: kMinFontSize,
              max: kMaxFontSize,
              divisions: 20,
              activeColor: AppColors.primary,
              onChanged: (v) =>
                  context.read<SettingsBloc>().add(ChangeFontSizeEvent(v)),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('${kMinFontSize.toInt()}pt',
                    style: const TextStyle(
                        fontSize: 12, color: AppColors.textSecondary)),
                Text('${kMaxFontSize.toInt()}pt',
                    style: const TextStyle(
                        fontSize: 12, color: AppColors.textSecondary)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _AboutTile extends StatelessWidget {
  const _AboutTile();

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.zero,
      child: Column(
        children: [
          ListTile(
            leading: const Icon(Icons.info_outline_rounded),
            title: const Text('BibleLib'),
            subtitle: const Text('Version 1.0.0'),
          ),
          const Divider(height: 1, indent: 16, endIndent: 16),
          ListTile(
            leading: const Icon(Icons.code_rounded),
            title: const Text('Powered by API.Bible'),
            subtitle: const Text('rest.api.bible'),
          ),
          const Divider(height: 1, indent: 16, endIndent: 16),
          const ListTile(
            leading: Icon(Icons.copyright_rounded),
            title: Text(kAppCredits),
          ),
        ],
      ),
    );
  }
}
