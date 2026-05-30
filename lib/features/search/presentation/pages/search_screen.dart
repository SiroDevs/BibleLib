// lib/features/search/presentation/pages/search_screen.dart

import 'package:biblelib/core/theme/app_colors.dart';
import 'package:flutter/material.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _controller = TextEditingController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        titleSpacing: 0,
        title: TextField(
          controller: _controller,
          autofocus: true,
          decoration: const InputDecoration(
            hintText: 'Search the Bible...',
            border: InputBorder.none,
            enabledBorder: InputBorder.none,
            focusedBorder: InputBorder.none,
            hintStyle: TextStyle(color: AppColors.textSecondary),
            contentPadding: EdgeInsets.symmetric(horizontal: 8, vertical: 14),
          ),
          style: Theme.of(context).textTheme.bodyLarge,
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.close_rounded),
            onPressed: () {
              if (_controller.text.isNotEmpty) {
                _controller.clear();
              } else {
                Navigator.pop(context);
              }
            },
          ),
        ],
      ),
      body: const _SearchEmptyState(),
    );
  }
}

class _SearchEmptyState extends StatelessWidget {
  const _SearchEmptyState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.search_rounded,
            size: 80,
            color: AppColors.primary.withOpacity(0.3),
          ),
          const SizedBox(height: 16),
          Text(
            'Search Coming Soon',
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Full-text Bible search will be available\nin a future update.',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }
}
