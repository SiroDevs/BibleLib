import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../core/constants/app_constants.dart';
import '../../../core/di/service_locator.dart';
import '../../../core/theme/app_colors.dart';
import '../../bloc/reader/reader_bloc.dart';
import '../../bloc/reader/reader_event.dart';
import '../../bloc/reader/reader_state.dart';
import '../../widgets/chapter_nav_hint.dart';
import '../../widgets/reader_app_bar.dart';
import '../../widgets/verse_tile.dart';
import '../search/search_screen.dart';
import '../settings/settings_screen.dart';

class ReaderScreen extends StatelessWidget {
  const ReaderScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => sl<ReaderBloc>()..add(const InitReaderEvent()),
      child: const _ReaderView(),
    );
  }
}

class _ReaderView extends StatefulWidget {
  const _ReaderView();

  @override
  State<_ReaderView> createState() => _ReaderViewState();
}

class _ReaderViewState extends State<_ReaderView> {
  double _baseScaleFontSize = kDefaultFontSize_;
  double _currentFontSize = kDefaultFontSize_;

  void _onScaleStart(ScaleStartDetails details, ReaderLoaded state) {
    _baseScaleFontSize = state.fontSize;
  }

  void _onScaleUpdate(ScaleUpdateDetails details, BuildContext context) {
    if (details.pointerCount < 2) return;
    final newSize =
        (_baseScaleFontSize * details.scale).clamp(kMinFontSize, kMaxFontSize);
    if ((newSize - _currentFontSize).abs() > 0.5) {
      _currentFontSize = newSize;
      context.read<ReaderBloc>().add(UpdateFontSizeEvent(newSize));
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<ReaderBloc, ReaderState>(
      builder: (context, state) {
        if (state is ReaderLoaded) {
          _currentFontSize = state.fontSize;
        }

        return Scaffold(
          appBar: state is ReaderLoaded
              ? ReaderAppBar(
                  chapter: state.chapter,
                  onSearchTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const SearchScreen()),
                  ),
                  onBookmarkTap: () {},
                  onSettingsTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(builder: (_) => const SettingsScreen()),
                  ),
                )
              : AppBar(title: const Text(kAppName)),
          body: _buildBody(context, state),
        );
      },
    );
  }

  Widget _buildBody(BuildContext context, ReaderState state) {
    if (state is ReaderInitial || state is ReaderLoading) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.primary),
      );
    }

    if (state is ReaderError) {
      return _ErrorView(
        message: state.message,
        onRetry: () =>
            context.read<ReaderBloc>().add(const InitReaderEvent()),
      );
    }

    if (state is ReaderLoaded) {
      return GestureDetector(
        onScaleStart: (d) => _onScaleStart(d, state),
        onScaleUpdate: (d) => _onScaleUpdate(d, context),
        onHorizontalDragEnd: (details) {
          const threshold = 200.0;
          final velocity = details.primaryVelocity ?? 0;
          if (velocity < -threshold) {
            context.read<ReaderBloc>().add(const NavigateNextChapterEvent());
          } else if (velocity > threshold) {
            context
                .read<ReaderBloc>()
                .add(const NavigatePreviousChapterEvent());
          }
        },
        child: _VerseListView(state: state),
      );
    }

    return const SizedBox.shrink();
  }
}

class _VerseListView extends StatelessWidget {
  final ReaderLoaded state;

  const _VerseListView({required this.state});

  @override
  Widget build(BuildContext context) {
    return CustomScrollView(
      physics: const BouncingScrollPhysics(),
      slivers: [
        SliverToBoxAdapter(
          child: ChapterNavHint(
            hasNext: state.chapter.hasNext,
            hasPrevious: state.chapter.hasPrevious,
          ),
        ),
        SliverList.builder(
          itemCount: state.verses.length,
          itemBuilder: (context, index) => VerseTile(
            verse: state.verses[index],
            fontSize: state.fontSize,
          ),
        ),
        const SliverToBoxAdapter(child: _ChapterEndBar()),
      ],
    );
  }
}

class _ChapterEndBar extends StatelessWidget {
  const _ChapterEndBar();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<ReaderBloc, ReaderState>(
      builder: (context, state) {
        if (state is! ReaderLoaded) return const SizedBox.shrink();
        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 24),
          child: Row(
            children: [
              if (state.chapter.hasPrevious)
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => context
                        .read<ReaderBloc>()
                        .add(const NavigatePreviousChapterEvent()),
                    icon: const Icon(Icons.arrow_back_ios_new_rounded, size: 14),
                    label: const Text('Previous'),
                  ),
                ),
              if (state.chapter.hasPrevious && state.chapter.hasNext)
                const SizedBox(width: 12),
              if (state.chapter.hasNext)
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => context
                        .read<ReaderBloc>()
                        .add(const NavigateNextChapterEvent()),
                    icon: const Text('Next'),
                    label: const Icon(Icons.arrow_forward_ios_rounded, size: 14),
                  ),
                ),
            ],
          ),
        );
      },
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
            const Icon(Icons.error_outline_rounded, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            Text(
              message,
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodyLarge,
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh_rounded),
              label: const Text('Retry'),
            ),
          ],
        ),
      ),
    );
  }
}
