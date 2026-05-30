// lib/features/selection/presentation/widgets/download_progress_widget.dart

import 'package:biblelib/core/theme/app_colors.dart';
import 'package:flutter/material.dart';

class DownloadProgressWidget extends StatelessWidget {
  final int completed;
  final int total;
  final String currentBibleName;

  const DownloadProgressWidget({
    super.key,
    required this.completed,
    required this.total,
    required this.currentBibleName,
  });

  double get _progress => total == 0 ? 0 : completed / total;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.download_rounded,
              size: 64,
              color: AppColors.primary,
            ),
            const SizedBox(height: 24),
            Text(
              'Setting up your library',
              style: theme.textTheme.headlineMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            if (currentBibleName.isNotEmpty)
              Text(
                currentBibleName,
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: AppColors.textSecondary,
                ),
                textAlign: TextAlign.center,
              ),
            const SizedBox(height: 32),
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: LinearProgressIndicator(
                value: _progress,
                backgroundColor: AppColors.primary.withOpacity(0.15),
                valueColor:
                    const AlwaysStoppedAnimation<Color>(AppColors.primary),
                minHeight: 10,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              '$completed of $total bibles configured',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: AppColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
