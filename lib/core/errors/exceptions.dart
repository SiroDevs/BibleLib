class ServerException implements Exception {
  final String message;
  const ServerException(this.message);
}

class NetworkException implements Exception {
  final String message;
  const NetworkException(this.message);
}

class CacheException implements Exception {
  final String message;
  const CacheException(this.message);
}

class NotFoundException implements Exception {
  final String message;
  const NotFoundException(this.message);
}

class UnauthorizedException implements Exception {
  final String message;
  const UnauthorizedException(this.message);
}
