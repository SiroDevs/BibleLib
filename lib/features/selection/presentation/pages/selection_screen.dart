// lib/features/selection/presentation/pages/selection_screen.dart

import 'package:biblelib/core/di/service_locator.dart';
import 'package:biblelib/core/extensions/context_extensions.dart';
import 'package:biblelib/core/theme/app_colors.dart';
import 'package:biblelib/features/reader/presentation/pages/reader_screen.dart';
import 'package:biblelib/features/selection/presentation/bloc/selection_bloc.dart';
import 'package:biblelib/features/selection/presentation/bloc/selection_event.dart';
import 'package:biblelib/features/selection/presentation/bloc/selection_state.dart';
import 'package:biblelib/features/selection/presentation/widgets/bible_list_item.dart';
import 'package:biblelib/features/selection/presentation/widgets/download_progress_widget.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class SelectionScreen extends StatelessWidget {
  const SelectionScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => sl<SelectionBloc>()
        ..add(const LoadAvailableBiblesEvent()),
      child: const _SelectionView(),
    );
  }
}

class _SelectionView extends StatelessWidget {
  const _SelectionView();

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<SelectionBloc, SelectionState>(
      listener: (context, state) {
        if (state is SelectionDone) {
          Navigator.pushAndRemoveUntil(
            context,
            MaterialPageRoute(builder: (_) => const ReaderScreen()),
            (route) => false,
          );
        }
        if (state is SelectionError) {
          context.showSnackBar(state.message, isError: true);
        }
      },
      builder: (context, state) {
        if (state is SelectionDownloading) {
          return Scaffold(
            body: DownloadProgressWidget(
              completed: state.completed,
              total: state.total,
              currentBibleName: state.currentBibleName,
            ),
          );
        }

        return Scaffold(
          appBar: AppBar(
            title: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Choose Your Bibles',
                  style: context.textTheme.titleLarge,
                ),
                Text(
                  'Select one or more translations',
                  style: context.textTheme.bodyMedium?.copyWith(
                    color: AppColors.textSecondary,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
            toolbarHeight: 70,
          ),
          body: _buildBody(context, state),
          bottomNavigationBar: _buildBottomBar(context, state),
        );
      },
    );
  }

  Widget _buildBody(BuildContext context, SelectionState state) {
    if (state is SelectionLoading || state is SelectionInitial) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.primary),
      );
    }

    if (state is SelectionError) {
      return _ErrorView(
        message: state.message,
        onRetry: () =>
            context.read<SelectionBloc>().add(const LoadAvailableBiblesEvent()),
      );
    }

    if (state is SelectionLoaded) {
      return Column(
        children: [
          _SearchBar(bibles: state.bibles),
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.only(bottom: 100),
              itemCount: state.bibles.length,
              itemBuilder: (context, index) {
                final bible = state.bibles[index];
                return BibleListItem(
                  bible: bible,
                  isSelected: state.selectedIds.contains(bible.id),
                  onTap: () => context
                      .read<SelectionBloc>()
                      .add(ToggleBibleSelectionEvent(bible.id)),
                );
              },
            ),
          ),
        ],
      );
    }

    return const SizedBox.shrink();
  }

  Widget _buildBottomBar(BuildContext context, SelectionState state) {
    if (state is! SelectionLoaded) return const SizedBox.shrink();
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: AnimatedOpacity(
          opacity: state.hasSelection ? 1.0 : 0.4,
          duration: const Duration(milliseconds: 200),
          child: ElevatedButton(
            onPressed: state.hasSelection
                ? () => context
                    .read<SelectionBloc>()
                    .add(const ConfirmSelectionEvent())
                : null,
            child: Text(
              state.hasSelection
                  ? 'Continue with ${state.selectedIds.length} Bible${state.selectedIds.length == 1 ? '' : 's'}'
                  : 'Select at least one Bible',
            ),
          ),
        ),
      ),
    );
  }
}

class _SearchBar extends StatefulWidget {
  final List bibles;
  const _SearchBar({required this.bibles});

  @override
  State<_SearchBar> createState() => _SearchBarState();
}

class _SearchBarState extends State<_SearchBar> {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
      child: TextField(
        decoration: InputDecoration(
          hintText: 'Search translations...',
          prefixIcon: const Icon(Icons.search, color: AppColors.textSecondary),
          hintStyle:
              const TextStyle(color: AppColors.textSecondary, fontSize: 14),
          contentPadding: const EdgeInsets.symmetric(vertical: 12),
        ),
        onChanged: (_) {},
      ),
    );
  }
}

class _ErrorView extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;

  const _ErrorView({required this.message, required this.onRetry});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.wifi_off_rounded, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            Text(
              message,
              textAlign: TextAlign.center,
              style: context.textTheme.bodyLarge,
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('Try Again'),
            ),
          ],
        ),
      ),
    );
  }
}
