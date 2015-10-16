'use strict';

angular.module('grungleApp')
    .controller('MainController', function ($scope, Principal, $state) {
        Principal.identity().then(function (account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });

        if (!Principal.isAuthenticated()) $state.go("login");
    });
