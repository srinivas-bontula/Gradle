function GetTaxConfig($scope, $http) {
	$http.get('/taxproService/rest/taxservices/gettaxconfig')
			.success(function(data) {
				$scope.configs = data.raterMap;
			});
}