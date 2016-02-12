/* @flow */
export type LoginResult = {
  isCancelled: boolean;
  tokenString?: string;
  grantedPermissions?: Array<string>;
  declinedPermissions?: Array<string>;
}
