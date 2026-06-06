import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../domain/entities/verse_entity.dart';

class VerseTile extends StatelessWidget {
  final VerseEntity verse;
  final double fontSize;

  const VerseTile({
    super.key,
    required this.verse,
    required this.fontSize,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
      child: RichText(
        text: TextSpan(
          children: [
            WidgetSpan(
              alignment: PlaceholderAlignment.top,
              child: Padding(
                padding: const EdgeInsets.only(right: 6, top: 2),
                child: Text(
                  '${verse.verseNumber}',
                  style: TextStyle(
                    fontSize: fontSize * 0.72,
                    fontWeight: FontWeight.bold,
                    color: isDark
                        ? AppColors.verseNumberDark
                        : AppColors.verseNumber,
                  ),
                ),
              ),
            ),
            TextSpan(
              text: verse.content.trim(),
              style: TextStyle(
                fontSize: fontSize,
                height: 1.75,
                color: isDark
                    ? AppColors.textPrimaryDark
                    : AppColors.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
