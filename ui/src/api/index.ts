import { axiosInstance } from '@halo-dev/api-client'
import { PostShareLinkV1alpha1Api } from './generated'

const postShareApiClient = {
  postShare: new PostShareLinkV1alpha1Api(undefined, '', axiosInstance)
}

export { postShareApiClient }
