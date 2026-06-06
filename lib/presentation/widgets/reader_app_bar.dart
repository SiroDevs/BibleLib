import 'package:flutter/material.dart';

import '../../core/extensions/string_extensions.dart';
import '../../core/theme/app_colors.dart';
import '../../domain/models/chapter_model.dart';

class ReaderAppBar extends StatelessWidget implements PreferredSizeWidget {
  final ChapterModel chapter;
  final VoidCallback onSearchTap;
  final VoidCallback onBookmarkTap;
  final VoidCallback onSettingsTap;

  const ReaderAppBar({
    super.key,
    required this.chapter,
    required this.onSearchTap,
    required this.onBookmarkTap,
    required this.onSettingsTap,
  });

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return AppBar(
      title: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            chapter.bookId.bookName,
            style: Theme.of(context)
                .textTheme
                .titleMedium
                ?.copyWith(fontWeight: FontWeight.bold),
          ),
          Text(
            'Chapter ${chapter.number}',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  fontSize: 12,
                  color: isDark
                      ? AppColors.textSecondaryDark
                      : AppColors.textSecondary,
                ),
          ),
        ],
      ),
      toolbarHeight: kToolbarHeight,
      actions: [
        IconButton(
          icon: const Icon(Icons.search_rounded),
          tooltip: 'Search',
          onPressed: onSearchTap,
        ),
        PopupMenuButton<String>(
          icon: const Icon(Icons.more_vert_rounded),
          onSelected: (value) {
            if (value == 'bookmark') onBookmarkTap();
            if (value == 'settings') onSettingsTap();
          },
          itemBuilder: (_) => [
            const PopupMenuItem(
              value: 'bookmark',
              child: ListTile(
                leading: Icon(Icons.bookmark_border_rounded),
                title: Text('Bookmark'),
                subtitle: Text('Coming soon'),
                dense: true,
                contentPadding: EdgeInsets.zero,
              ),
            ),
            const PopupMenuItem(
              value: 'settings',
              child: ListTile(
                leading: Icon(Icons.settings_outlined),
                title: Text('Settings'),
                dense: true,
                contentPadding: EdgeInsets.zero,
              ),
            ),
          ],
        ),
      ],
    );
  }
}
