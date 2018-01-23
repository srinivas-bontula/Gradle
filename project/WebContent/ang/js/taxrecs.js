function GetTaxRecs($scope, $http) {
	$http.get('/taxproService/rest/taxrecservices/getallrecs')
			.success(function(data) {
				$scope.taxrecs = data;
			});
}

function GetRecs($scope, $http) {
	$scope.pageCfg = {
		page : 0,
		count : 3
	};
    $scope.loadPage = function() {
        $http.get('/taxproService/rest/taxrecservices/getrecs/tax/' + $scope.pageCfg.page + '/' + $scope.pageCfg.count).success(function(data){
            $scope.taxrecs = data.content;
            $scope.pageCfg.pages = data.totalPages;
        });
    };
    $scope.loadPage();
    $scope.nextPage = function() {
        if ($scope.pageCfg.page < $scope.pageCfg.pages) {
            $scope.pageCfg.page++;
            $scope.loadPage();
        }
    };
    
    $scope.previousPage = function() {
        if ($scope.pageCfg.page > 0) {
            $scope.pageCfg.page--;
            $scope.loadPage();
        }
    };	
}