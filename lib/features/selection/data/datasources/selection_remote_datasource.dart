// lib/features/selection/data/datasources/selection_remote_datasource.dart

import 'package:biblelib/core/errors/exceptions.dart';
import 'package:biblelib/core/network/api_client.dart';
import 'package:biblelib/features/selection/data/models/bible_model.dart';

abstract class SelectionRemoteDataSource {
  Future<List<BibleModel>> getAvailableBibles();
}

class SelectionRemoteDataSourceImpl implements SelectionRemoteDataSource {
  final ApiClient _client;

  const SelectionRemoteDataSourceImpl(this._client);

  @override
  Future<List<BibleModel>> getAvailableBibles() async {
    try {
      final response = await _client.get(
        '/bibles',
        queryParameters: {'include-full-details': false},
      );
      final data = response['data'] as List<dynamic>? ?? [];
      return data
          .cast<Map<String, dynamic>>()
          .map(BibleModel.fromJson)
          .toList();
    } catch (e) {
      if (e is ServerException || e is NetworkException) rethrow;
      throw ServerException(e.toString());
    }
  }
}
