import '../../../core/errors/exceptions.dart';
import '../../../core/network/api_client.dart';
import '../../entities/bible_entity.dart';

abstract class SelectionRemoteDataSource {
  Future<List<BibleEntity>> getAvailableBibles();
}

class SelectionRemoteDataSourceImpl implements SelectionRemoteDataSource {
  final ApiClient _client;

  const SelectionRemoteDataSourceImpl(this._client);

  @override
  Future<List<BibleEntity>> getAvailableBibles() async {
    try {
      final response = await _client.get(
        '/bibles',
        queryParameters: {'include-full-details': false},
      );
      final data = response['data'] as List<dynamic>? ?? [];
      return data
          .cast<Map<String, dynamic>>()
          .map(BibleEntity.fromJson)
          .toList();
    } catch (e) {
      if (e is ServerException || e is NetworkException) rethrow;
      throw ServerException(e.toString());
    }
  }
}
