var App = angular.module('App');

App.controller('AppCtrl', function($scope, $state, $ionicPopup, AuthService, AUTH_EVENTS, UserDataService) {
  $scope.username = AuthService.username();

  $scope.$on(AUTH_EVENTS.notAuthenticated, function(event) {
    AuthService.logout();
    $state.go('login');
    var alertPopup = $ionicPopup.alert({
      title: 'Session Lost!',
      template: 'Sorry, You have to login again.'
    });
  });
});

App.controller('LoginCtrl', function($scope, $state, $ionicPopup, AuthService, UserDataService) {
  $scope.data = {};

  $scope.login = function(data) {
    AuthService.login(data.username, data.password).then(function(authenticated) {
      UserDataService.refresh();
      $state.go('main.dash', {}, {reload: true});
    }, function(err) {
      var alertPopup = $ionicPopup.alert({
        title: 'Login failed!',
        template: 'Please check your credentials!'
      });
    });
  };
});

App.controller('RegisterCtrl', function($scope, $state, $http, $ionicPopup, API) {
  $scope.register = function(data) {
    var request = {'username': data.username, 'password': data.password, 'email': data.email};
    API.post('user/createUser', request, 
      function(response) {
        window.history.back();
        $ionicPopup.alert({
          title: 'Success',
          template: 'Please login with your username and password.'
        });
      }, 
      function(response) {
        $ionicPopup.alert({
          title: 'Error',
          template: 'Problem creating a user. Please Try again later.'
        });
      }
    );
  };

  $scope.goBack = function() {
    window.history.back();
  };
});

App.controller('CreateEventCtrl', function($scope, $http, $ionicPopup, UserDataService, AuthService, API) {
  $scope.friends = UserDataService.getFriends();
  $scope.data = {};

  (function () {
    $scope.$watch(function () {
      return UserDataService.getFriends();
    }, function (newValue, oldValue) {
      if ( newValue !== oldValue ) {
          $scope.friends = newValue;
      }
    });
  }());

  $scope.goBack = function() {
    window.history.back();
  };

  $scope.createEvent = function() {
    var data = this.data;
    var request = {
      'name': data.eventName,
      'description': data.eventDescription,
      'location': data.eventLocation,
      'startDate': (1900 + data.eventDate.getYear()) + '-' + data.eventDate.getMonth() + '-' + data.eventDate.getDate(),
      'host': AuthService.username(),
      'invitees': data.guests
    };

    API.post('event/createEvent', request,
      function(response) {
        window.history.back();
        $ionicPopup.alert({
          title: 'Success',
          template: 'Event created!'
        });
      },
      function(response) {
        $ionicPopup.alert({
          title: 'Error',
          template: 'Please try again later.'
        });
      }
    );
  };
});

App.controller('DashCtrl', function($scope, $state, AuthService, UserDataService, $ionicPopup, $ionicLoading, API) {

  $scope.model = {};
  $scope.model.events = UserDataService.getEvents();
  $scope.model.invites = UserDataService.getInvites();

  $scope.username = AuthService.username();
  UserDataService.refresh();

  (function () {
    $scope.$watch(function () {
      return UserDataService.getEvents();
    }, function (newValue, oldValue) {
      if ( newValue !== oldValue ) {
          $scope.model.events = newValue;
      }
    });
  }());

  (function () {
    $scope.$watch(function () {
      return UserDataService.getInvites();
    }, function (newValue, oldValue) {
      if ( newValue !== oldValue ) {
          $scope.model.invites = newValue;
      }
    });
  }());

  $scope.logout = function() {
    AuthService.logout();
    $state.go('login');
  };

  $scope.refresh = function() {
  	UserDataService.refresh();
  };

  $scope.createEvent = function() {
    $state.go('create');
  };

  $scope.acceptInvite = function(eventId) {
    UserDataService.acceptInvite(eventId);
  };

  $scope.declineInvite = function(eventId) {
    UserDataService.declineInvite(eventId);
  };

  $scope.findTime = function(eventId) {
    $ionicLoading.show({
      content: 'Loading',
      animation: 'fade-in',
      showBackdrop: true,
      maxWidth: 200,
      showDelay: 0
    });

    var request = {'id': eventId};
    API.post('event/findTime', request, 
      function (response) {
        $ionicLoading.hide();
        $scope.presentOption(response.data);
      }, function (response) {
        console.log(response);
      }
    );
  };

  $scope.presentOption = function(tempEvent) {
    var eventStart = new Date(tempEvent.startDate);
    var eventEnd = new Date(tempEvent.endDate);
    var time = '' + eventStart.toDateString() + ' from ' + eventStart.getHours() + ':' + eventStart.getMinutes() + ' to ' + eventEnd.getHours() + ':' + eventEnd.getMinutes();

    $ionicPopup.confirm({
      title: 'Free time found!',
      template: 'Everyone is free on ' + time + '.<br>Finalize this event? This will remove any pending invitations.'
    }).then(function (agree) {
      if (agree) { 
        UserDataService.finalizeEvent(tempEvent.id);
      }
    });
  };
});

App.controller('FriendsController', function($scope, UserDataService, AuthService, API, $state, $http, $ionicPopup, $cordovaCalendar) {
  
  $scope.data = {};
  $scope.username = AuthService.username();
  $scope.userService = UserDataService;

  $scope.model = {};
  $scope.model.friends = UserDataService.getFriends();

  (function () {
    $scope.$watch(function () {
      return UserDataService.getFriends();
    }, function (newValue, oldValue) {
      if ( newValue !== oldValue ) {
        $scope.model.friends = newValue;
      }
    });
  }());

  // Ignore this.
  $scope.testCalendar = function() {
    var x;
    var y;
    var from = new Date();
    var to = new Date();
    to.setDate(from + 31);
    $cordovaCalendar.listEventsInRange(
      from,
      to
    ).then(function (result) {
      x = result;
    }, function (err) {
      y = err;
    });
  };

  $scope.addFriend = function(friend) {
    var username = AuthService.username();
    var request = [{'username': username}, {'username': friend}];
    API.post('/user/addFriend', request, 
      function(response) {
        UserDataService.refresh();
        $ionicPopup.alert({
          title: 'Success',
          template: response
        });
      },
      function(response) {
        $ionicPopup.alert({
          title: 'Error',
          template: response
        });
      }
    );
  }

  $scope.removeFriend = function(friend) {
    var username = AuthService.username();
    var request = [{'username': username}, {'username': friend}];
    API.post('user/removeFriend', request, 
      function(response) {
        UserDataService.refresh();
        $ionicPopup.alert({
          title: 'Success',
          template: response
        });
      },
      function(response) {
        $ionicPopup.alert({
          title: 'Error',
          template: response
        });
      }
    );
  };
});

App.controller('SettingsController', function($scope, $cordovaFacebook, UserDataService) {
  $scope.linkFB = function() {
    $cordovaFacebook.login(['user_events']).then( function (user) {
      console.log(user);
    }, function (error) {
      console.log(error);
    });
    console.log('linked with fb!');
  };

  $scope.getStatus = function() {
    $cordovaFacebook.getLoginStatus().then(
      function (success) {
        console.log('user is currently logged in');
        console.log(success);
        console.log('getting credentials');
        $cordovaFacebook.getAccessToken().then(
          function (token) {
            console.log('userToken is');
            console.log(token);
          }, function (failure) {
            console.log('couldnt retrieve token');
            console.log(failure);
          }
        );
      }, function (failure) {
        console.log('user is NOT logged in');
        console.log(failure);
      }
    );
  };
});