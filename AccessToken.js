/* @flow */
export type AccessToken = {
  expiresAt: number;
  lastRefreshedAt: ?number;
  userId: string;
  applicationId: string;
  tokenString: string;
  grantedPermissions: Array<string>;
  declinedPermissions: Array<string>;
}
