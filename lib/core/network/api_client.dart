// lib/core/network/api_client.dart

import 'package:biblelib/core/constants/app_constants.dart';
import 'package:biblelib/core/errors/exceptions.dart';
import 'package:dio/dio.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

class ApiClient {
  late final Dio _dio;

  ApiClient() {
    _dio = Dio(
      BaseOptions(
        baseUrl: dotenv.env['BIBLE_API_BASE_URL'] ?? kApiBaseUrl,
        connectTimeout: const Duration(milliseconds: kApiConnectTimeout),
        receiveTimeout: const Duration(milliseconds: kApiReceiveTimeout),
        headers: {
          'api-key': dotenv.env['BIBLE_API_KEY'] ?? '',
          'Accept': 'application/json',
        },
      ),
    );

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) => handler.next(options),
        onResponse: (response, handler) => handler.next(response),
        onError: (DioException e, handler) {
          switch (e.type) {
            case DioExceptionType.connectionTimeout:
            case DioExceptionType.receiveTimeout:
            case DioExceptionType.sendTimeout:
              throw const NetworkException('Connection timed out');
            case DioExceptionType.badResponse:
              _handleHttpError(e.response?.statusCode ?? 0);
            case DioExceptionType.connectionError:
              throw const NetworkException('No internet connection');
            default:
              throw ServerException(e.message ?? 'Unknown server error');
          }
          handler.next(e);
        },
      ),
    );
  }

  void _handleHttpError(int statusCode) {
    switch (statusCode) {
      case 401:
        throw const UnauthorizedException('Invalid or missing API key');
      case 403:
        throw const UnauthorizedException('Access to this Bible is forbidden');
      case 404:
        throw const NotFoundException('Resource not found');
      case 400:
        throw const ServerException('Bad request');
      default:
        throw ServerException('Server error: $statusCode');
    }
  }

  Future<Map<String, dynamic>> get(
    String path, {
    Map<String, dynamic>? queryParameters,
  }) async {
    try {
      final response = await _dio.get<Map<String, dynamic>>(
        path,
        queryParameters: queryParameters,
      );
      return response.data ?? {};
    } on DioException catch (e) {
      _handleHttpError(e.response?.statusCode ?? 0);
      rethrow;
    }
  }

  Dio get rawDio => _dio;
}
